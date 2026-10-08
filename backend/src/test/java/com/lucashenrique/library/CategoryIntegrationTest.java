package com.lucashenrique.library;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @Transactional
class CategoryIntegrationTest {
    @Autowired MockMvc mvc;
    @Test void createNormalizesAndCanBeRead() throws Exception {
        mvc.perform(post("/api/categories").contentType("application/json").content("{\"name\":\"  História  \"}"))
            .andExpect(status().isCreated()).andExpect(header().exists("Location")).andExpect(jsonPath("$.name").value("História"));
        mvc.perform(get("/api/categories")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].name").value("História"));
    }
    @Test void duplicateCategoryUsesDatabaseCollation() throws Exception {
        mvc.perform(post("/api/categories").contentType("application/json").content("{\"name\":\"Ficção\"}"))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/categories").contentType("application/json").content("{\"name\":\"ficcao\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value("Já existe uma categoria com esse nome."));
    }
    @Test void updateAndDeleteCategory() throws Exception {
        String body = mvc.perform(post("/api/categories").contentType("application/json").content("{\"name\":\"Ciência\"}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body).get("id").asLong();
        mvc.perform(put("/api/categories/" + id).contentType("application/json").content("{\"name\":\"  Ciências  \"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Ciências"));
        mvc.perform(delete("/api/categories/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/categories/" + id)).andExpect(status().isNotFound());
    }
    @Test void rejectsBlankName() throws Exception {
        mvc.perform(post("/api/categories").contentType("application/json").content("{\"name\":\"   \"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].field").value("name"));
    }
    @Test void rejectsInvalidPagination() throws Exception {
        mvc.perform(get("/api/categories?size=101")).andExpect(status().isBadRequest());
    }
    @Test void reportsMissingCategory() throws Exception {
        mvc.perform(get("/api/categories/999999")).andExpect(status().isNotFound());
    }
}
