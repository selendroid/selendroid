package io.selendroid;

import io.selendroid.ServerInstrumentation.InputEventSender;
import io.selendroid.exceptions.SelendroidException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static io.selendroid.util.Preconditions.checkState;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Provides facility to inject base-level UI operations (such as KeyEvent's and MotionEvent's)
 * with more advanced synchronization than android Instrumentation class does.
 *
 * The key point is to loop UI thread 'manually', while waiting for the injection to be complete.
 * This allows it to fetch and propagate unhandled exceptions, occurring in application under test
 * and causing a crash, to the local end.
 */
public class UiThreadController implements Handler.Callback {

  private static final int INPUT_EVENT_INJECTION_COMPLETED = 0x1;

  private static final Method MESSAGE_QUEUE_NEXT;
  private final ExecutorService keyEventExecutor = Executors.newSingleThreadExecutor();
  private final Looper mainLooper = Looper.getMainLooper();
  private Handler controllerHandler = new Handler(this);
  private boolean shouldWaitForInputEventCompletion;
  private boolean loopingUiThread;
  private int messageGeneration;

  static {
    try {
      MESSAGE_QUEUE_NEXT = MessageQueue.class.getDeclaredMethod("next");
      MESSAGE_QUEUE_NEXT.setAccessible(true);
    } catch (Exception e) {
      throw new SelendroidException("Unable to fetch MessageQueue.next() method.");
    }
  }

  public void injectInputEventWaitingForCompletion(final InputEventSender eventSender,
                                                   final Object event) {
    // We really do need instance equality
    checkState(Looper.myLooper() == mainLooper, "Should be called on UI thread!");

    final FutureTask<Void> injectTask = new InjectingTask<Void>(
        new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            eventSender.sendEvent(event);
            return null;
          }
        },
        messageGeneration);

    keyEventExecutor.submit(injectTask);
    loopUiThreadUntilInjectionComplete();

    try {
      injectTask.get();
    } catch (Exception e) {
      throw new SelendroidException("Event injection failed.", e);
    }
  }

  @Override
  public boolean handleMessage(Message message) {
    if (message.what == INPUT_EVENT_INJECTION_COMPLETED && message.arg1 == messageGeneration) {
      shouldWaitForInputEventCompletion = false;
      return true;
    }
    return false;
  }

  private void loopUiThreadUntilInjectionComplete() {
    checkState(!loopingUiThread, "Already looping UI thread.");
    loopingUiThread = true;
    shouldWaitForInputEventCompletion = true;
    try {
      long waitUntil = SystemClock.uptimeMillis() + MINUTES.toMillis(1);
      while (SystemClock.uptimeMillis() < waitUntil) {
        if (!shouldWaitForInputEventCompletion) {
          return;
        }
        Message message = invokeMessageQueueNextMethod();
        message.getTarget().dispatchMessage(message);
        message.recycle();
      }
    } finally {
      loopingUiThread = false;
      shouldWaitForInputEventCompletion = false;
      messageGeneration++;
    }
  }

  private Message invokeMessageQueueNextMethod() {
    try {
      return (Message) MESSAGE_QUEUE_NEXT.invoke(Looper.myQueue());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Encapsulates posting a signal message to indicate that
   * injection has executed.
   */
  private class InjectingTask<T> extends FutureTask<T> {

    private final int myGeneration;

    public InjectingTask(Callable<T> callable, int myGeneration) {
      super(callable);
      this.myGeneration = myGeneration;
    }

    @Override
    protected void done() {
      controllerHandler.sendMessage(Message.obtain(controllerHandler,
          INPUT_EVENT_INJECTION_COMPLETED, myGeneration, 0, null));
    }
  }
}
