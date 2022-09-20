package com.game.service;

import com.game.util.FilterForm;
import com.game.entity.Player;
import com.game.repository.PlayersRepository;
import com.game.repository.specification.PlayerSpecification;
import com.game.repository.specification.SearchCriteria;
import com.game.repository.specification.SearchOperation;
import com.game.util.PlayerIdNotValid;
import com.game.util.PlayerNotFoundException;
import com.game.util.PlayerWasNotCreated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlayersService {
    private final PlayersRepository playersRepository;
    @Autowired
    public PlayersService(PlayersRepository playersRepository) {
        this.playersRepository = playersRepository;
    }

    public List<Player> findAll(FilterForm filterForm, Pageable pageable){
        return playersRepository.findAll(convertToSpecification(filterForm), pageable).getContent();
    }

    public List<Player> findAll(FilterForm filterForm){
        return playersRepository.findAll(convertToSpecification(filterForm));
    }

    public Player findById(String id){
        return playersRepository.findById(validateId(id)).orElseThrow(PlayerNotFoundException::new);
    }

    public Player update(String id, Player player){
        player.setId(validateId(id));

        Player updatedPlayer = playersRepository.findById(player.getId()).orElseThrow(PlayerNotFoundException::new);
        if (player.getName() != null) updatedPlayer.setName(player.getName());
        if (player.getTitle() != null) updatedPlayer.setTitle(player.getTitle());
        if (player.getRace() != null) updatedPlayer.setRace(player.getRace());
        if (player.getProfession() != null) updatedPlayer.setProfession(player.getProfession());
        if (player.getBirthday() != null) updatedPlayer.setBirthday(player.getBirthday());
        if (player.getBanned() != null) updatedPlayer.setBanned(player.getBanned());
        if (player.getExperience() != null) updatedPlayer.setExperience(player.getExperience());
        return save(updatedPlayer);
    }

    @Transactional
    public Player save(Player player){
        validatePlayer(player);
        if (player.getBanned() == null) player.setBanned(false);
        return playersRepository.saveAndFlush(setLevels(player));
    }

    @Transactional
    public void delete(String id){
        findById(id);
        playersRepository.deleteById(Long.parseLong(id));
    }

    private void validatePlayer(Player player) {
        if (player.getRace() == null || player.getProfession() == null || player.getBirthday() == null) throw new PlayerWasNotCreated();
        if (player.getName() == null || player.getName().isEmpty() || player.getName().length() > 12)
            throw new PlayerWasNotCreated();
        if (player.getTitle() == null || player.getTitle().isEmpty() || player.getTitle().length() > 30)
            throw new PlayerWasNotCreated();
        if (player.getExperience() == null || player.getExperience() < 0 || player.getExperience() > 10_000_000)
            throw new PlayerWasNotCreated();
        if (player.getBirthday().getYear() < 100 || player.getBirthday().getYear() > 1100)
            throw new PlayerWasNotCreated();
    }

    private Long validateId(String id) {
        if(id == null || id.equals("") || id.equals("0") || id.contains(".") || id.contains("-")) throw new PlayerIdNotValid();
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new PlayerIdNotValid();
        }
    }

    private Player setLevels(Player player){
        Integer level = (int)((Math.sqrt(2500 + 200 * player.getExperience())) - 50) / 100;
        player.setLevel(level);
        player.setUntilNextLevel(50 * (level + 1) * (level + 2) - player.getExperience());
        return player;
    }

    private Specification<Player> convertToSpecification(FilterForm filterForm) {
        PlayerSpecification specification = new PlayerSpecification();

        if (filterForm.getRace() != null) specification.add(new SearchCriteria("race", filterForm.getRace(), SearchOperation.EQUAL));
        if (filterForm.getProfession() != null) specification.add(new SearchCriteria("profession", filterForm.getProfession(), SearchOperation.EQUAL));
        if (filterForm.getBanned() != null) specification.add(new SearchCriteria("banned", filterForm.getBanned(), SearchOperation.EQUAL));
        if (filterForm.getName() != null) specification.add(new SearchCriteria("name", filterForm.getName(), SearchOperation.MATCH));
        if (filterForm.getTitle() != null) specification.add(new SearchCriteria("title", filterForm.getTitle(), SearchOperation.MATCH));
        if (filterForm.getAfter() != null) specification.add(new SearchCriteria("birthday", new Date(filterForm.getAfter()), SearchOperation.DATE_GREATER_THAN));
        if (filterForm.getBefore() != null) specification.add(new SearchCriteria("birthday", new Date(filterForm.getBefore()), SearchOperation.DATE_LESS_THAN));
        if (filterForm.getMaxLevel() != null) specification.add(new SearchCriteria("level", filterForm.getMaxLevel(), SearchOperation.LESS_THAN_EQUAL));
        if (filterForm.getMinLevel() != null) specification.add(new SearchCriteria("level", filterForm.getMinLevel(), SearchOperation.GREATER_THAN_EQUAL));
        if (filterForm.getMaxExperience() != null) specification.add(new SearchCriteria("experience", filterForm.getMaxExperience(), SearchOperation.LESS_THAN_EQUAL));
        if (filterForm.getMinExperience() != null) specification.add(new SearchCriteria("experience", filterForm.getMinExperience(), SearchOperation.GREATER_THAN_EQUAL));
        return specification;
    }
}
