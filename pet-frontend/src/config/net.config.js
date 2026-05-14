const network = {
  baseURL:
    process.env.NODE_ENV === "production"
      ? "https://situationship.icu/api"
      : "http://localhost:8080/api",
  contentType: "application/json;charset=UTF-8",
  messageDuration: 3000,
  requestTimeout: 15000,
  successCode: [200, 0],
  invalidCode: 402,
  noPermissionCode: 401,
  useMock: process.env.NODE_ENV !== "production",
};

module.exports = network
