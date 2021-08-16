package com.example.demo.controllers;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.fasterxml.jackson.core.type.TypeReference;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.net.URI;
import static org.junit.Assert.*;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class UserControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    private User user;

    @Before
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("test-user");
        user.setPassword("password");
        given(userRepository.findById(user.getId())).willReturn(java.util.Optional.of(user));
        given(userRepository.findByUsername(user.getUsername())).willReturn(user);
    }

    @Test
    @WithMockUser(username="test")
    public void testFindByIdSuccess() throws Exception {
        MvcResult response = mvc.perform(get(new URI("/api/user/id/1")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();
        String result = response.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        User resultUser = mapper.readValue(result, new TypeReference<User>(){});
        assertEquals(user.getId(), resultUser.getId());
        assertEquals(user.getUsername(), resultUser.getUsername());
    }

    @Test
    @WithMockUser(username="test")
    public void testFindByIdInvalidUserId() throws Exception {
        mvc.perform(get(new URI("/api/user/id/10")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="test")
    public void findByUserNameSuccess() throws Exception {
        MvcResult response = mvc.perform(get(new URI("/api/user/" + user.getUsername())))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();
        String result = response.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        User resultUser = mapper.readValue(result, new TypeReference<User>(){});
        assertEquals(user.getId(), resultUser.getId());
        assertEquals(user.getUsername(), resultUser.getUsername());
    }

    @Test
    @WithMockUser(username="test")
    public void findByUserNameInvalidUserName() throws Exception {
        mvc.perform(get(new URI("/api/user/invalid-user-name")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="test")
    public void createUserSuccess() throws Exception {
        CreateUserRequest newUser = new CreateUserRequest();
        newUser.setUsername("new-user");
        newUser.setPassword("password");
        newUser.setConfirmPassword("password");
        MvcResult response = mvc.perform(post(new URI("/api/user/create"))
                        .content(ow.writeValueAsString(newUser))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();
        String result = response.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        User resultUser = mapper.readValue(result, new TypeReference<User>(){});
        assertEquals(0, resultUser.getId());
        assertEquals(newUser.getUsername(), resultUser.getUsername());
    }

    @Test
    @WithMockUser(username="test")
    public void createUserInvalidPasswordLength() throws Exception {
        CreateUserRequest newUser = new CreateUserRequest();
        newUser.setUsername("new-user");
        newUser.setPassword("pass");
        newUser.setConfirmPassword("pass");
        mvc.perform(post(new URI("/api/user/create"))
                .content(ow.writeValueAsString(newUser))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username="test")
    public void createUserInvalidConfirmPassword() throws Exception {
        CreateUserRequest newUser = new CreateUserRequest();
        newUser.setUsername("new-user");
        newUser.setPassword("password");
        newUser.setConfirmPassword("pass");
        mvc.perform(post(new URI("/api/user/create"))
                .content(ow.writeValueAsString(newUser))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());
    }
}
