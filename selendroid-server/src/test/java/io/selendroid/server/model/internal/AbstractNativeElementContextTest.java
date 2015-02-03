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

    /**
     * Currently selendroid searches all top levels views, ordered by the one
     * that was last painted first. If this happens we should first return
     * all the matching children from the first (visible?) tree, then from the
     * second one, and so on.
     *
     *           a                       g
     *        /     \                 /     \
     *       b       c              h        I (3)
     *    /     \      \         /     \      \
     *   D(1)    e      F (2)   J(4)    k      L (5)
     */
    @Test
    public void itReturnsTheChildrenInTheOrderOfTheRoots() {
        // Bottom 1
        ViewGroup D = match();
        ViewGroup e = ignore();
        ViewGroup F = match();

        // Middle 1
        ViewGroup b = ignore(D, e);
        ViewGroup c = ignore(F);

        // Root 1
        ViewGroup root1 = ignore(b, c);

        // Bottom 2
        ViewGroup J = match();
        ViewGroup k = ignore();
        ViewGroup L = match();

        // Middle 2
        ViewGroup h = ignore(J, k);
        ViewGroup I = match(L);

        // Root 2
        ViewGroup root2 = ignore(h, I);

        List<View> views = searchViews(root1, root2);
        assertFoundViews(views, D, F, I, J, L);
    }

    /**
     * The first hierarchy has no match, but the second one has.
     *
     *           a                       g
     *        /     \                 /     \
     *       b       c              h        I (1)
     *    /     \      \         /     \      \
     *   d       e      F       J(2)    k      L (3)
     */
    @Test
    public void itReturnsChildrenFromTheSecondRoot() {
        // Bottom 1
        ViewGroup d = ignore();
        ViewGroup e = ignore();
        ViewGroup f = ignore();

        // Middle 1
        ViewGroup b = ignore(d, e);
        ViewGroup c = ignore(f);

        // Root 1
        ViewGroup root1 = ignore(b, c);

        // Bottom 2
        ViewGroup J = match();
        ViewGroup k = ignore();
        ViewGroup L = match();

        // Middle 2
        ViewGroup h = ignore(J, k);
        ViewGroup I = match(L);

        // Root 2
        ViewGroup root2 = ignore(h, I);

        List<View> views = searchViews(root1, root2);
        assertFoundViews(views, I, J, L);
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
    private static List<View> searchViews(View ...roots) {
        List<View> rootViews = new ArrayList<View>(Arrays.asList(roots));
        List<AndroidElement> elements = AbstractNativeElementContext.searchViews(
                mockContext(),
                rootViews,
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
