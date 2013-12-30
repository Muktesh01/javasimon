package org.javasimon.demoapp.web;

import com.google.gson.*;
import org.javasimon.demoapp.dao.ToDoItemDao;
import org.javasimon.demoapp.model.ToDoItem;
import org.springframework.ui.ModelMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Ivan Mushketyk (ivan.mushketyk@gmail.com)
 */
public class ToDoControllerTest {

    private ToDoController controller;
    private ToDoItemDao dao;

    private Gson gson = new Gson();

    @BeforeMethod
    public void beforeMethod() {
        dao = mock(ToDoItemDao.class);
        controller = new ToDoController();
        controller.setToDoItemDao(dao);
    }

    @Test
    public void getAll() {
        List<ToDoItem> items = new ArrayList<ToDoItem>();
        ToDoItem item = new ToDoItem();
        item.setDone(false);
        item.setDescription("Item description");
        item.setName("Item name");
        items.add(item);

        when(dao.getAll()).thenReturn(items);

        JsonArray expectedArray = new JsonArray();
        JsonObject object = new JsonObject();
        object.addProperty("isDone", false);
        object.addProperty("name", "Item name");
        object.addProperty("description", "Item description");
        object.addProperty("id", 0);

        expectedArray.add(object);

        String str = controller.getAll(new ModelMap());
        JsonElement actualJson = parseJson(str);
        assertEquals(expectedArray, actualJson);
    }

    private JsonElement parseJson(String str) {
        return new JsonParser().parse(str);
    }

}

