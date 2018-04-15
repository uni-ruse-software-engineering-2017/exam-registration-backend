package uniruse.mse.examregistration.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import uniruse.mse.examregistration.user.model.ApplicationUser;

import static java.util.Collections.emptyList;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<ApplicationUser> appUser = this.userService.getByUsername(username);

        if (!appUser.isPresent()) {
            throw new UsernameNotFoundException(username);
        }

        User user = new User(appUser.get().getUsername(), appUser.get().getPassword(), emptyList());
        return user;
    }
}