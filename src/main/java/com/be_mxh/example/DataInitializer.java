package com.be_mxh.example;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        User user = new User();
        user.setUsername("lam123");
        user.setEmail("lam@gmail.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole("USER");

        userRepository.save(user);
    }
}