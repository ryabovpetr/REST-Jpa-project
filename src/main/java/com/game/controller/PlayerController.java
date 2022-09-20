package com.game.controller;

import com.game.util.FilterForm;
import com.game.entity.Player;
import com.game.service.PlayersService;
import com.game.util.PlayerErrorResponse;
import com.game.util.PlayerIdNotValid;
import com.game.util.PlayerNotFoundException;
import com.game.util.PlayerWasNotCreated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {
    private final PlayersService playersService;
    @Autowired
    public PlayerController(PlayersService playersService) {
        this.playersService = playersService;
    }
    @GetMapping()
    public List<Player> players(@RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "3") int pageSize,
                                @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder order,
                                @ModelAttribute FilterForm filterForm){
        return playersService.findAll(filterForm, PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName())));
    }

    @GetMapping("/count")
    public int countOfPlayers(@ModelAttribute FilterForm filterForm){
        return playersService.findAll(filterForm).size();
    }
    @GetMapping("/{id}")
    public Player show(@PathVariable("id") String id) {
        return playersService.findById(id);
    }

    @PostMapping()
    public Player create(@RequestBody Player player){
        return playersService.save(player);
    }

    @PostMapping("/{id}")
    public Player update(@PathVariable("id") String id, @RequestBody Player player) {
        return playersService.update(id, player);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id) {
        playersService.delete(id);
    }

    @ExceptionHandler
    private ResponseEntity<PlayerErrorResponse> handleException(PlayerNotFoundException e){
        PlayerErrorResponse response = new PlayerErrorResponse("Player with this ID wasn't found!");
        return new ResponseEntity(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    private ResponseEntity<PlayerErrorResponse> handleException(PlayerIdNotValid e){
        PlayerErrorResponse response = new PlayerErrorResponse("Player ID isn't valid!");
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<PlayerErrorResponse> handleException(PlayerWasNotCreated e){
        PlayerErrorResponse response = new PlayerErrorResponse("Player wasn't created / updated, because his parameters aren't valid!");
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }


}

