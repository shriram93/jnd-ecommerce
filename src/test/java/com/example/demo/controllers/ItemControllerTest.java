package com.example.demo.controllers;

import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRepository itemRepository;

    private Item item;

    @Before
    public void setup() {
        item = new Item();
        item.setId(1L);
        item.setName("widget");
        item.setPrice(new BigDecimal("2.99"));
        item.setDescription("A widget that is round");
        given(itemRepository.findById(item.getId())).willReturn(java.util.Optional.of(item));
        List<Item> items = new LinkedList<>();
        items.add(item);
        given(itemRepository.findByName(item.getName())).willReturn(items);
        given(itemRepository.findAll()).willReturn(items);
    }

    @Test
    @WithMockUser(username="test")
    public void getItemsSuccess() throws Exception {
        MvcResult response = mvc.perform(get(new URI("/api/item")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();
        String result = response.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        List<Item> items = mapper.readValue(result, new TypeReference<List<Item>>(){});
        Item resultItem = items.get(0);
        assertEquals(resultItem.getId(), item.getId());
        assertEquals(resultItem.getName(), item.getName());
        assertEquals(resultItem.getDescription(), item.getDescription());
        assertEquals(resultItem.getPrice(), item.getPrice());
    }

    @Test
    @WithMockUser(username="test")
    public void getItemByIdSuccess() throws Exception {
        MvcResult response = mvc.perform(get(new URI("/api/item/1")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();
        String result = response.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        Item resultItem = mapper.readValue(result, new TypeReference<Item>(){});
        assertEquals(resultItem.getId(), item.getId());
        assertEquals(resultItem.getName(), item.getName());
        assertEquals(resultItem.getDescription(), item.getDescription());
        assertEquals(resultItem.getPrice(), item.getPrice());
    }

    @Test
    @WithMockUser(username="test")
    public void getItemByIdInvalidId() throws Exception {
        mvc.perform(get(new URI("/api/item/10")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="test")
    public void getItemByNameSuccess() throws Exception {
        MvcResult response = mvc.perform(get(new URI("/api/item/name/" + item.getName())))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();
        String result = response.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        List<Item> items = mapper.readValue(result, new TypeReference<List<Item>>(){});
        Item resultItem = items.get(0);
        assertEquals(resultItem.getId(), item.getId());
        assertEquals(resultItem.getName(), item.getName());
        assertEquals(resultItem.getDescription(), item.getDescription());
        assertEquals(resultItem.getPrice(), item.getPrice());
    }

    @Test
    @WithMockUser(username="test")
    public void getItemByIdInvalidItemName() throws Exception {
        mvc.perform(get(new URI("/api/item/name/invalid-item-name")))
                .andExpect(status().isNotFound());
    }
}
