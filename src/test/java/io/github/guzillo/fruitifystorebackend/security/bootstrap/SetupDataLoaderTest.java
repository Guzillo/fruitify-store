package io.github.guzillo.fruitifystorebackend.security.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.guzillo.fruitifystorebackend.security.model.Privilege;
import io.github.guzillo.fruitifystorebackend.security.model.Role;
import io.github.guzillo.fruitifystorebackend.security.repository.PrivilegeRepository;
import io.github.guzillo.fruitifystorebackend.security.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SetupDataLoaderTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PrivilegeRepository privilegeRepository;

    @InjectMocks
    private SetupDataLoader setupDataLoader;

    @Test
    void shouldCreateDefaultPrivilegesAndRoles() {
        Privilege read = new Privilege("READ_PRIVILEGE");
        Privilege write = new Privilege("WRITE_PRIVILEGE");
        when(privilegeRepository.findByName("READ_PRIVILEGE")).thenReturn(null);
        when(privilegeRepository.findByName("WRITE_PRIVILEGE")).thenReturn(null);
        when(privilegeRepository.save(any())).thenReturn(read, write);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(null);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(null);

        setupDataLoader.onApplicationEvent(null);

        verify(privilegeRepository, org.mockito.Mockito.times(2)).save(any(Privilege.class));
        verify(roleRepository, org.mockito.Mockito.times(2)).save(any(Role.class));
    }

    @Test
    void shouldNotCreateExistingPrivilegesAndRoles() {
        Privilege read = new Privilege("READ_PRIVILEGE");
        Privilege write = new Privilege("WRITE_PRIVILEGE");
        when(privilegeRepository.findByName("READ_PRIVILEGE")).thenReturn(read);
        when(privilegeRepository.findByName("WRITE_PRIVILEGE")).thenReturn(write);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(new Role("ROLE_ADMIN"));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(new Role("ROLE_USER"));

        setupDataLoader.onApplicationEvent(null);

        verify(privilegeRepository, never()).save(any());
        verify(roleRepository, never()).save(any());
        assertThat(setupDataLoader.alreadySetup).isTrue();
    }

    @Test
    void shouldSkipSetupAfterFirstRun() {
        setupDataLoader.alreadySetup = true;

        setupDataLoader.onApplicationEvent(null);

        verifyNoInteractions();
    }

    private void verifyNoInteractions() {
        org.mockito.Mockito.verifyNoInteractions(roleRepository, privilegeRepository);
    }
}
