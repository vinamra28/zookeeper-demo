package com.example.zookeeperdemo.controller;

import com.example.zookeeperdemo.dto.Response;
import com.example.zookeeperdemo.service.LockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping(value = "/api")
public class LockController {

    @Autowired
    private LockService lockService;

    @PostMapping("/lock")
    public ResponseEntity<Response> lock(@RequestParam String value) {
        log.info("value to be locked: "+value);

        HttpStatus statusCode = HttpStatus.OK;
        Response response = new Response();
        boolean lockStatus = false;

        try {
            lockStatus = lockService.setLock(value);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setError(e.getMessage());
            statusCode = HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(response, statusCode);
        }

        response.setSuccess(lockStatus);
        response.setData(value + "locked successfully");
        return new ResponseEntity<>(response, statusCode);
    }

    @PostMapping("/unlock")
    public ResponseEntity<Response> unlock(@RequestParam String value) {
        log.info("value to be unlocked: "+value);

        HttpStatus statusCode = HttpStatus.OK;
        Response response = new Response();
        boolean lockStatus = false;

        try {
            lockStatus = lockService.unlock(value);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setError(e.getMessage());
            statusCode = HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(response, statusCode);
        }

        response.setSuccess(lockStatus);
        response.setData(value + "unlocked successfully");
        return new ResponseEntity<>(response, statusCode);
    }

}
