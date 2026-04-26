package com.franklintju.streamlab.category;

import com.franklintju.streamlab.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类", description = "视频分类管理")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取根分类", description = "获取所有一级分类")
    @GetMapping
    public ApiResponse<List<Category>> getAllRootCategories() {
        return ApiResponse.success(categoryService.getAllRootCategories());
    }

    @Operation(summary = "获取子分类", description = "获取指定分类的子分类")
    @GetMapping("/{id}/subcategories")
    public ApiResponse<List<Category>> getSubCategories(@PathVariable Long id) {
        return ApiResponse.success(categoryService.getSubCategories(id));
    }

    @Operation(summary = "获取分类", description = "根据ID获取分类详情")
    @GetMapping("/{id}")
    public ApiResponse<Category> getById(@PathVariable Long id) {
        return ApiResponse.success(categoryService.getById(id));
    }

    @Operation(summary = "创建分类", description = "创建新分类（仅管理员）")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Category> create(@RequestBody Category request) {
        return ApiResponse.success(categoryService.create(request));
    }

    @Operation(summary = "更新分类", description = "更新分类信息（仅管理员）")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Category> update(@PathVariable Long id, @RequestBody Category request) {
        return ApiResponse.success(categoryService.update(id, request));
    }

    @Operation(summary = "删除分类", description = "删除分类（仅管理员）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success(null);
    }
}
