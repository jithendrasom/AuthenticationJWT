package guru.springframework.controller;

import guru.springframework.config.JwtGeneratorInterface;
import guru.springframework.exception.UserNotFoundException;
import guru.springframework.model.User;
import guru.springframework.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1/user")
public class UserController {
    private UserService userService;
    private JwtGeneratorInterface jwtGenerator;

    @Autowired
    public UserController(UserService userService, JwtGeneratorInterface jwtGenerator){
        this.userService=userService;
        this.jwtGenerator=jwtGenerator;
    }

    @PostMapping("/register")
    public ResponseEntity<?> postUser(@RequestBody User user){
        try{
            userService.saveUser(user);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/login")
   // public ResponseEntity<?> loginUser(@RequestBody User user, @RequestParam Map<String, String> jwtToken) {
    public ResponseEntity<?> loginUser(@RequestBody User user, @RequestParam(required = false) String jwtToken) {
        try {
            if(StringUtils.isNotBlank(jwtToken)){
                return new ResponseEntity<>(jwtToken, HttpStatus.OK);
            } else {
                if (user.getUserName() == null || user.getPassword() == null) {
                    throw new UserNotFoundException("UserName or Password is Empty");
                }
                User userData = userService.getUserByNameAndPassword(user.getUserName(), user.getPassword());
                if (userData == null) {
                    throw new UserNotFoundException("UserName or Password is Invalid");
                }
            }
            Map<String, String> jwtTokenGen = new HashMap<>();
            // return new ResponseEntity<>(jwtGenerator.generateToken(user), HttpStatus.OK);

            jwtToken = Jwts.builder().setSubject("myToken").setIssuedAt(new Date()).setIssuer("Jithen")
                    .signWith(SignatureAlgorithm.HS512, "secret").compact();

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Authorization",  jwtToken);

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body("Response with auth header");

        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }
}
