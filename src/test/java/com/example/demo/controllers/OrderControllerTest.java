package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class OrderControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OrderRepository orderRepository;

    private Cart cart;

    private User user;

    private Item item;

    private UserOrder order;

    @Before
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("password");
        cart = new Cart();
        user.setCart(cart);

        item = new Item();
        item.setId(1L);
        item.setName("widget");
        item.setPrice(new BigDecimal("2.99"));
        item.setDescription("A widget that is round");
        List<Item> items = new LinkedList<>();
        items.add(item);

        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(items);
        cart.setTotal(item.getPrice());

        order = new UserOrder();
        order.setId(1L);
        order.setTotal(cart.getTotal());
        order.setItems(cart.getItems());
        order.setUser(user);

        given(userRepository.findByUsername(user.getUsername())).willReturn(user);
        given(orderRepository.save(ArgumentMatchers.any())).willReturn(order);
        List<UserOrder> orders = new LinkedList<>();
        orders.add(order);
        given(orderRepository.findByUser(ArgumentMatchers.any())).willReturn(orders);
    }

    @Test
    @WithMockUser(username="test")
    public void submitOrder() throws Exception {
        MvcResult response = mvc.perform(post(new URI("/api/order/submit/" + user.getUsername()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();
        String result = response.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        UserOrder resultOrder = mapper.readValue(result, new TypeReference<UserOrder>(){});
        assertEquals(resultOrder.getTotal(), order.getTotal());
        assertEquals(resultOrder.getUser().getUsername(), order.getUser().getUsername());
        assertEquals(resultOrder.getUser().getId(), order.getUser().getId());
        Item resultItem = resultOrder.getItems().get(0);
        assertEquals(resultItem.getId(), item.getId());
        assertEquals(resultItem.getPrice(), item.getPrice());
        assertEquals(resultItem.getDescription(), item.getDescription());
        assertEquals(resultItem.getName(), item.getName());
    }

    @Test
    @WithMockUser(username="test")
    public void submitOrderInvalidUserName() throws Exception {
        mvc.perform(post(new URI("/api/order/submit/invalid-user-name")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="test")
    public void getOrdersForUserSuccess() throws Exception {
        MvcResult response = mvc.perform(get(new URI("/api/order/history/" + user.getUsername()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();
        String result = response.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        List<UserOrder> orders = mapper.readValue(result, new TypeReference<List<UserOrder>>(){});
        UserOrder resultOrder = orders.get(0);
        assertEquals(resultOrder.getTotal(), order.getTotal());
        assertEquals(resultOrder.getUser().getUsername(), order.getUser().getUsername());
        assertEquals(resultOrder.getUser().getId(), order.getUser().getId());
        Item resultItem = resultOrder.getItems().get(0);
        assertEquals(resultItem.getId(), item.getId());
        assertEquals(resultItem.getPrice(), item.getPrice());
        assertEquals(resultItem.getDescription(), item.getDescription());
        assertEquals(resultItem.getName(), item.getName());
    }

    @Test
    @WithMockUser(username="test")
    public void getOrdersForUserInvalidUserName() throws Exception {
        mvc.perform(get(new URI("/api/order/history/invalid-user-name")))
                .andExpect(status().isNotFound());
    }
}
