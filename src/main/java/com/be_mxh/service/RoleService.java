package com.be_mxh.service;

import com.be_mxh.entity.Role;

public interface RoleService {
    Iterable<Role> findAll();

    void save(Role role);

    Role findByName(String name);
}
