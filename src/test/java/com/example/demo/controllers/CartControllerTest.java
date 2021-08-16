package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class CartControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ItemRepository itemRepository;

    private Cart cart;

    private User user;

    private Item item;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

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

        given(userRepository.findByUsername(user.getUsername())).willReturn(user);
        given(itemRepository.findById(item.getId())).willReturn(java.util.Optional.ofNullable(item));
    }

    @Test
    @WithMockUser(username="test")
    public void addToCartSuccess() throws Exception {
        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setUsername(user.getUsername());
        cartRequest.setItemId(item.getId());
        cartRequest.setQuantity(1);
        MvcResult response = mvc.perform(post(new URI("/api/cart/addToCart"))
                .content(ow.writeValueAsString(cartRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();
        String result = response.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        Cart resultCart = mapper.readValue(result, new TypeReference<Cart>(){});
        assertEquals(resultCart.getId(), cart.getId());
        assertEquals(resultCart.getUser().getUsername(), user.getUsername());
        assertEquals(resultCart.getUser().getId(), user.getId());
        assertEquals(resultCart.getTotal(), cart.getTotal());
        assertEquals(resultCart.getItems(), cart.getItems());
    }

    @Test
    @WithMockUser(username="test")
    public void addToCartInvalidUserName() throws Exception {
        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setUsername("invalid-user-name");
        mvc.perform(post(new URI("/api/cart/addToCart"))
                .content(ow.writeValueAsString(cartRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="test")
    public void addToCartInvalidItemId() throws Exception {
        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setUsername(user.getUsername());
        cartRequest.setItemId(2L);
        mvc.perform(post(new URI("/api/cart/addToCart"))
                .content(ow.writeValueAsString(cartRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="test")
    public void removeFromCartSuccess() throws Exception {
        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setUsername(user.getUsername());
        cartRequest.setItemId(item.getId());
        cartRequest.setQuantity(1);
        MvcResult response = mvc.perform(post(new URI("/api/cart/removeFromCart"))
                .content(ow.writeValueAsString(cartRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();
        String result = response.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        Cart resultCart = mapper.readValue(result, new TypeReference<Cart>(){});
        assertEquals(resultCart.getId(), cart.getId());
        assertEquals(resultCart.getUser().getUsername(), user.getUsername());
        assertEquals(resultCart.getUser().getId(), user.getId());
        assertEquals(resultCart.getTotal(), new BigDecimal("0.00"));
        assertEquals(resultCart.getItems().size(), 0);
    }

    @Test
    @WithMockUser(username="test")
    public void removeFromCartInvalidUserName() throws Exception {
        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setUsername("invalid-user-name");
        mvc.perform(post(new URI("/api/cart/removeFromCart"))
                .content(ow.writeValueAsString(cartRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="test")
    public void removeFromCartInvalidItemId() throws Exception {
        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setUsername(user.getUsername());
        cartRequest.setItemId(2L);
        mvc.perform(post(new URI("/api/cart/removeFromCart"))
                .content(ow.writeValueAsString(cartRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }

}
