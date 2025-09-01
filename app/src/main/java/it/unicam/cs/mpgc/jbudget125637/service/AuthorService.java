package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.Author;
import it.unicam.cs.mpgc.jbudget125637.persistency.UserXmlRepository;
import java.util.List;
import java.util.stream.Collectors;

public class AuthorService {
    private final UserXmlRepository userRepository;

    public AuthorService() {
        this.userRepository = new UserXmlRepository();
    }

    public List<String> getAllAuthorNames() {
        return userRepository.read().stream()
                .map(Author::name)
                .collect(Collectors.toList());
    }
}