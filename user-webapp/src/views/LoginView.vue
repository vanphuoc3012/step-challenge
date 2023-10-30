<script lang="ts" setup>
import { ref } from "vue";
import axios from "axios";
import DataStore from "../DataStore.ts";

const username = ref("");
const password = ref("");
const alertMessage = ref("");

function login() {
  if (!username.value || !password.value) {
    return;
  }
  axios.post("http://localhost:4000/api/v1/token", {
    username: username.value,
    password: password.value
  }).then(res => {
    DataStore.setToken(res.data);
    DataStore.setUsername(username.value);
  });
}
</script>

<template>
  <div>
    <div v-if="alertMessage" class="alert alert-danger" role="alert">
      {{ alertMessage }}
    </div>
    <form @submit="login">
      <div class="form-group">
        <label for="username">User name</label>
        <input id="username" v-model="username" class="form-control" placeholder="somebody123" type="username">
      </div>
      <div class="form-group">
        <label for="password">Password</label>
        <input id="password" v-model="password" class="form-control" placeholder="abc123" type="password">
      </div>
      <button class="btn btn-primary" type="submit">Submit</button>
    </form>
    <div>
      <p>...or
        <router-link to="/register">register</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>

</style>