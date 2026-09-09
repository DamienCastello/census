package fr.castello.census.security;

import fr.castello.census.entity.Role;
import fr.castello.census.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSecurityService implements UserDetailsService {
    @Autowired
    private PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username.equals("john")){
            return new User(username, encoder.encode("123"), List.of(Role.USER));
        }
        else if (username.equals("mozinor")){
            return new User(username, encoder.encode("000"), List.of(Role.ADMIN));
        }
        else {
            throw new UsernameNotFoundException("Informations fournis incorrects");
        }
    }
}
