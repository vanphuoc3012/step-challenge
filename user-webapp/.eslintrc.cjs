module.exports = {
  env: {
    "browser": true,
    "es2021": true,
    "node": true
  },
  extends: [
    "eslint:recommended",
    "plugin:vue/vue3-recommended",
    "prettier"
  ],
  parser: "vue-eslint-parser",
  parserOptions: {
    ecmaVersion: "latest",
    sourceType: "module",
    parser: "@typescript-eslint/parser"
  },
  rules: {
    "semi": ["error", "always"],
    "quotes": ["error", "double"]
  }
};