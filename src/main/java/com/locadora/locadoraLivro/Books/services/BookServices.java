package com.locadora.locadoraLivro.Books.services;

import com.locadora.locadoraLivro.Books.DTOs.CreateBookRequestDTO;
import com.locadora.locadoraLivro.Books.DTOs.UpdateBookRecordDTO;
import com.locadora.locadoraLivro.Books.Validation.BookValidation;
import com.locadora.locadoraLivro.Books.models.BookModel;
import com.locadora.locadoraLivro.Books.repositories.BookRepository;
import com.locadora.locadoraLivro.Exceptions.ModelNotFoundException;
import com.locadora.locadoraLivro.Publishers.models.PublisherModel;
import com.locadora.locadoraLivro.Publishers.repositories.PublisherRepository;
import com.locadora.locadoraLivro.Rents.models.RentModel;
import com.locadora.locadoraLivro.Rents.repositories.RentRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BookServices {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    RentRepository rentRepository;

    @Autowired
    BookValidation bookValidation;


    public ResponseEntity<Void> create(@Valid CreateBookRequestDTO data) {

        bookValidation.validLaunchDate(data);
        bookValidation.validTotalQuantity(data);

        PublisherModel publisher = publisherRepository.findById(data.publisherId())
                .orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
        bookValidation.validPublisherExist(data);

        BookModel newBook = new BookModel(data.name(), data.author(), data.launchDate(), data.totalQuantity(), publisher);
        bookRepository.save(newBook);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public List<BookModel> findAll() {
        List<BookModel> book = bookRepository.findAllByIsDeletedFalse();
        if (book.isEmpty()) throw new ModelNotFoundException();
        return book;
    }


    public Optional<BookModel> findById(int id){
        return bookRepository.findById(id);
    }

    public ResponseEntity<Object> update(int id, @Valid UpdateBookRecordDTO updateBookRecordDTO){
        Optional<BookModel> response = bookRepository.findById(id);
        if (response.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");

        bookValidation.validTotalQuantityUpdate(updateBookRecordDTO);
        bookValidation.validLaunchDateUpdate(updateBookRecordDTO);

        PublisherModel publisher = publisherRepository.findById(updateBookRecordDTO.publisherId())
                .orElseThrow(() -> new IllegalArgumentException("Publisher not found"));

        var bookModel = response.get();
        bookModel.setName(updateBookRecordDTO.name());
        bookModel.setAuthor(updateBookRecordDTO.author());
        bookModel.setLaunchDate(updateBookRecordDTO.launchDate());
        bookModel.setTotalQuantity(updateBookRecordDTO.totalQuantity());
        bookModel.setPublisher(publisher);

        bookRepository.save(bookModel);
        return ResponseEntity.status(HttpStatus.OK).body(bookModel);
    }

    public ResponseEntity<Object> delete(int id){
        Optional<BookModel> response = bookRepository.findById(id);
        if (response.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");

        bookValidation.validDeleteBook(id);

        BookModel book = response.get();

        book.setDeleted(true);

        bookRepository.save(book);

        return ResponseEntity.status(HttpStatus.OK).body("Book deleted successfully");
    }
}
