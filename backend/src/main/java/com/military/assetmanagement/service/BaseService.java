package com.military.assetmanagement.service;

import com.military.assetmanagement.dto.request.BaseRequest;
import com.military.assetmanagement.entity.Base;
import com.military.assetmanagement.exception.BadRequestException;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.BaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BaseService {

    private final BaseRepository baseRepository;

    public List<Base> findAll() {
        return baseRepository.findAll();
    }

    public Base findById(Long id) {
        return baseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Base not found with id: " + id));
    }

    public Base create(BaseRequest request) {
        if (baseRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("A base with this name already exists");
        }
        Base base = new Base();
        base.setName(request.getName());
        base.setLocation(request.getLocation());
        return baseRepository.save(base);
    }

    public Base update(Long id, BaseRequest request) {
        Base base = findById(id);
        base.setName(request.getName());
        base.setLocation(request.getLocation());
        return baseRepository.save(base);
    }

    public void delete(Long id) {
        Base base = findById(id);
        baseRepository.delete(base);
    }
}
