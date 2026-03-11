package com.franklintju.streamlab.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllRootCategories() {
        return categoryRepository.findByParentIsNullOrderBySortOrder();
    }

    public List<Category> getSubCategories(Long parentId) {
        return categoryRepository.findByParentIdOrderBySortOrder(parentId);
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, Category updated) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setName(updated.getName());
        category.setIcon(updated.getIcon());
        category.setSortOrder(updated.getSortOrder());
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
