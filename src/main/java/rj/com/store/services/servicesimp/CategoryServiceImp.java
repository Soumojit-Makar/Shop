package rj.com.store.services.servicesimp;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import rj.com.store.datatransferobjects.CategoryDTO;
import rj.com.store.datatransferobjects.PageableResponse;
import rj.com.store.enities.Category;
import rj.com.store.exceptions.ResourceNotFoundException;
import rj.com.store.helper.Helper;
import rj.com.store.repositories.CategoryRepository;
import rj.com.store.services.CategoryService;
import rj.com.store.services.ImageServiceInCloud;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
/**
 * Implementation of the CategoryService interface to handle
 * business logic related to categories.
 * This class provides methods to create, update, delete, and retrieve categories.
 */
@Service
public class CategoryServiceImp implements CategoryService {
    Logger logger= LoggerFactory.getLogger(CategoryServiceImp.class);
    /**
     * The CategoryRepository instance for performing CRUD operations on categories.
     */
    private final CategoryRepository categoryRepository;
    /**
     * The ModelMapper instance for converting between Category entities and DTOs.
     */
    private  final ModelMapper mapper;
    /**
     * The ImageServiceInCloud instance for managing category avatars in Cloudinary
     */
    private final ImageServiceInCloud imageServiceInCloud;
    /**
     * Constructor for CategoryServiceImp.
     *
     * @param imageServiceInCloud The service for handling image uploads and deletions in the cloud.
     * @param mapper The ModelMapper instance for mapping between DTOs and entities.
     * @param categoryRepository The repository for category entity operations.
     */
    @Autowired
    public CategoryServiceImp( ImageServiceInCloud imageServiceInCloud, ModelMapper mapper, CategoryRepository categoryRepository) {
        this.imageServiceInCloud = imageServiceInCloud;
        this.mapper = mapper;
        this.categoryRepository = categoryRepository;
    }

    @Value("${category.cover.image.path}")
    String imageUploadPath;
    private static final String CATEGORY_NOT_FOUND="Category Not found exception.";

    /**
     * Creates a new category based on the CategoryDTO object.
     *
     * @param categoryDTO The CategoryDTO object containing the data for the new category.
     * @return The created CategoryDTO object.
     */
    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        categoryDTO.setCategoryId(UUID.randomUUID().toString());
        Category category=mapper.map(categoryDTO,Category.class);

       return mapper.map(
               categoryRepository
                       .save(category)
               ,CategoryDTO.class
       );
    }
    /**
     * Updates a category based on the CategoryDTO object and categoryId.
     *
     * @param categoryDTO The CategoryDTO object containing updated data.
     * @param categoryId  The ID of the category to update.
     * @throws ResourceNotFoundException if the category with the given ID is not found.
     * @return The updated CategoryDTO object.
     */
    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO,String categoryId) {
      Category category= categoryRepository.findById(categoryId).orElseThrow(()->new ResourceNotFoundException(CATEGORY_NOT_FOUND));
      category.setTitle(categoryDTO.getTitle());
      if (category.getCoverImage().equalsIgnoreCase(categoryDTO.getCoverImage())){
          category.setCoverImage(categoryDTO.getCoverImage());
      }
      else {
          imageServiceInCloud.deleteImage(category.getCoverImage());
          category.setCoverImage(categoryDTO.getCoverImage());
      }
      category.setCoverImage(categoryDTO.getCoverImage());
      category.setDescription(categoryDTO.getDescription());
      return mapper.map(categoryRepository.save(category),CategoryDTO.class);

    }
    /**
     * Deletes a category based on the categoryId.
     *
     * @param categoryId The ID of the category to delete.
     * @throws ResourceNotFoundException if the category with the given ID is not found.
     */
    @Override
    public void deleteCategory(String categoryId) {
        Category category= categoryRepository.findById(categoryId).orElseThrow(()->new ResourceNotFoundException(CATEGORY_NOT_FOUND));
        imageServiceInCloud.deleteImage(category.getCoverImage());
        categoryRepository.delete(category);
    }
    /**
     * Creates a pageable object that fetches all categories with pagination and sorting.
     *
     * @param pageSize   The number of items per page.
     * @param pageNumber The page number to retrieve (0-based).
     * @param sortBy     The field to sort by.
     * @param sortDir    The direction to sort (either "asc" or "desc").
     */

     @Override
    public PageableResponse<CategoryDTO> getAllCategory(int pageNumber, int pageSize, String sortBy, String sortDir) {
       Page<Category> page= categoryRepository.findAll(
                PageRequest
                        .of(pageNumber, pageSize,
                                (sortDir.equalsIgnoreCase("desc"))?
                                        (Sort.by(sortBy).descending())
                                        :
                                        (Sort.by(sortBy).ascending())
                        )
        );
        return Helper.getPageableResponse(page,CategoryDTO.class);
    }
    /**
     * Finds a category by its ID and returns it as a CategoryDTO.
     *
     * @param id The ID of the category to find.
     * @return A CategoryDTO object representing the category.
     * @throws ResourceNotFoundException if the category is not found.
     */
    @Override
    public CategoryDTO getCategoryById(String id) {
        Category category= categoryRepository.findById(id).orElseThrow(()->new ResourceNotFoundException(CATEGORY_NOT_FOUND));

        String fullPath=imageUploadPath+category.getCoverImage();
        try {

            Files.delete(Paths.get(fullPath));
        } catch (IOException e){
            logger.info("Exception is {}",e.getMessage());
        }
        return  mapper.map(category,CategoryDTO.class);
    }

    /**
     * Creates a pageable object with sorting options.
     * @param keyword   The keyword string for searching.
     * @param pageSize  The number of items per page.
     * @param pageNumber The page number to retrieve (0-based).
     * @param sortBy    The field to sort by.
     * @param sortDir   The direction to sort (either "asc" or "desc").
     * @return A Pageable object.
     */
    @Override
    public PageableResponse<CategoryDTO> searchCategory(String keyword, int pageNumber, int pageSize, String sortBy, String sortDir) {
      Page<Category>page=  categoryRepository.findByTitleContaining(keyword,
                PageRequest.of(pageNumber, pageSize,
                                (sortDir.equalsIgnoreCase("desc"))?
                                        (Sort.by(sortBy).descending())
                                        :
                                        (Sort.by(sortBy).ascending())
                        )
                ).orElseThrow(()->new ResourceNotFoundException("Category not found"));

        return Helper.getPageableResponse(page,CategoryDTO.class);
    }
}
