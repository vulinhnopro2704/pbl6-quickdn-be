//package com.pbl6.microservices.auth.client;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@FeignClient(value = "auth", url = "http://localhost:8082")
//public interface AuthClient {
//    @RequestMapping(method = RequestMethod.GET, value = "/api/auth/account")
//    boolean isActiveAccount(@RequestParam String email);
//}
