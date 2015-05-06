package io.selendroid.server;

import io.selendroid.server.action.ActionChain;
import io.selendroid.server.common.action.touch.TouchActionName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by colin on 4/19/15.
 */
public class ActionChainTest {

    @Test
    public void testCreateActionChain() throws JSONException {
        JSONObject actionJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject obj1 = new JSONObject();
        obj1.put("name", TouchActionName.POINTER_DOWN);
        jsonArray.put(obj1);
        actionJSON.put("actions", jsonArray);
        actionJSON.put("inputDevice", "TestDevice");
        ActionChain actionChain = new ActionChain(actionJSON);
        // Should not be paused, should have 1 item next
        Assert.assertTrue(actionChain.hasNext());
        JSONObject nextItem = actionChain.next();

        // Should not be paused, should not have next.
        Assert.assertEquals(nextItem.get("name"), TouchActionName.POINTER_DOWN);
        Assert.assertEquals(nextItem, obj1);
        Assert.assertFalse(actionChain.hasNext());
    }

    @Test
    public void testPauseAction() throws JSONException {
        JSONObject actionJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject obj1 = new JSONObject();
        obj1.put("name", TouchActionName.PAUSE);
        obj1.put("ms", 500);
        jsonArray.put(obj1);
        actionJSON.put("actions", jsonArray);
        actionJSON.put("inputDevice", "TestDevice");
        ActionChain actionChain = new ActionChain(actionJSON);
        // Should not be paused, should have 1 item next
        Assert.assertTrue(actionChain.hasNext());
        JSONObject nextItem = actionChain.next();

        // Should be paused, should not have next.
        Assert.assertEquals(nextItem.get("name"), TouchActionName.PAUSE);
        Assert.assertEquals(actionChain.getPauseTime(), 500);
        Assert.assertEquals(nextItem, obj1);
        Assert.assertFalse(actionChain.hasNext());
    }
}
