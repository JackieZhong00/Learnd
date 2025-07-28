package Learnd.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/recommend")
public class RecommendationController {

    @GetMapping("/getRecommendations/{deckId}")
    public void getRecommendations(@PathVariable("deckId") int deckId) {

    }
}
