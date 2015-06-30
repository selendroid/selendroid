package io.selendroid.server.model.internal;

import android.view.View;
import android.view.ViewGroup;
import com.android.internal.util.Predicate;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.server.model.AndroidNativeElement;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractNativeElementContextTest {
    final static int NO_MATCH = 0;
    final static int MATCH = 42;
    final static Predicate<View> FIND_MATCH = new TestIdPredicate(MATCH);

    /**
     * No nodes matches.
     *
     *    a
     *   / \
     *  b   c
     */
    @Test
    public void itFindsNothingIfNoNodeMatches() {
        ViewGroup b = ignore();
        ViewGroup c = ignore();
        ViewGroup a = ignore(b, c);

        List<View> views = searchViews(a);

        assertEquals(0, views.size());
    }

    /**
     * Root matches and no children.
     *
     *   A (1)
     */
    @Test
    public void itFindsTheRoot() {
        ViewGroup root = match();

        List<View> views = searchViews(root);

        assertFoundViews(views, root);
    }

    /**
     * Root, doesn't match, but children on various levels do.
     *
     *           a
     *        /     \
     *       b       C (1)
     *    /     \      \
     *   D(2)    e      F (3)
     */
    @Test
    public void itFindsChildrenOfTheRoot() {
        // Bottom
        ViewGroup D = match();
        ViewGroup e = ignore();
        ViewGroup F = match();

        // Middle
        ViewGroup b = ignore(D, e);
        ViewGroup C = match(F);

        // Root
        ViewGroup root = ignore(b, C);

        List<View> views = searchViews(root);
        assertFoundViews(views, C, D, F);
    }

    private static void assertFoundViews(List<View> foundViews, View ...expectedViewsInOrder) {
        assertEquals(expectedViewsInOrder.length, foundViews.size());

        for (int i = 0; i < expectedViewsInOrder.length; i++) {
            assertEquals(expectedViewsInOrder[i], foundViews.get(i));
        }
    }

    /**
     * @return A node that gets returned by the search
     */
    private ViewGroup match(ViewGroup ...children) {
        return mockViewGroup(MATCH, children);
    }

    /**
     * @return A node that does not get returned by the search
     */
    private ViewGroup ignore(ViewGroup ...children) {
        return mockViewGroup(NO_MATCH, children);
    }

    /**
     * Helper to do the actual searching.
     */
    private static List<View> searchViews(View root) {
        List<AndroidElement> elements = AbstractNativeElementContext.searchViews(
                    mockContext(),
                    root,
                    FIND_MATCH,
                    false);
        List<View> foundViews = new ArrayList<View>();
        for (AndroidElement element : elements) {
            foundViews.add(((AndroidNativeElement) element).getView());
        }
        return foundViews;
    }

    private static AbstractNativeElementContext mockContext() {
        AbstractNativeElementContext context = mock(AbstractNativeElementContext.class);
        when(context.newAndroidElement(any(View.class))).then(new Answer<AndroidElement>() {
            @Override
            public AndroidElement answer(InvocationOnMock invocationOnMock) throws Throwable {
                AndroidNativeElement element = mock(AndroidNativeElement.class);

                when(element.getView()).thenReturn((ViewGroup) invocationOnMock.getArguments()[0]);

                return element;
            }

        });
        return context;
    }

    static class TestIdPredicate implements Predicate<View> {
        private int id;
        protected TestIdPredicate(int id) {
            this.id = id;
        }

        @Override
        public boolean apply(View view) {
            return view.getId() == id;
        }
    }

    private ViewGroup mockViewGroup(int id, ViewGroup ...viewChildren) {
        ViewGroup view = mock(ViewGroup.class);
        final ArrayList<ViewGroup> children = new ArrayList<ViewGroup>(Arrays.asList(viewChildren));

        when(view.getId()).thenReturn(id);
        when(view.getChildCount()).thenReturn(children.size());
        when(view.getChildAt(anyInt())).then(new Answer<View>() {
            @Override
            public View answer(InvocationOnMock invocationOnMock) throws Throwable {
                return children.get((Integer) invocationOnMock.getArguments()[0]);
            }
        });

        return view;
    }
}
