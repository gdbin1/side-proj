package com.gbk.sideproj.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.gbk.sideproj.domain.Contact;
import java.util.List;

@Mapper
public interface ContactMapper {
    void insertContact(Contact contact);
    List<Contact> findAll();
}
