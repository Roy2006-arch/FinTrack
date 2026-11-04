package com.fintrack.service;
import com.fintrack.dto.Requests.TransactionInput;
import com.fintrack.entity.*;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.TransactionRepository;
import com.fintrack.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock TransactionRepository repository;
    @Mock CategoryService categories;
    @Mock UserService users;
    @InjectMocks TransactionServiceImpl service;
    private final User user = new User("Test", "test@example.com", "hash");
    private Category category;
    @BeforeEach void setup() { category = new Category(user, "Food", CategoryType.EXPENSE); ReflectionTestUtils.setField(category, "id", 2L); }
    private TransactionInput input(String amount) { return new TransactionInput(new BigDecimal(amount), TransactionType.EXPENSE, 2L, "Lunch", PaymentMethod.UPI, LocalDate.now()); }
    @Test void creationUsesAuthenticatedOwnerAndOwnedCategory() {
        when(users.require(1L)).thenReturn(user); when(categories.require(1L, 2L, TransactionType.EXPENSE)).thenReturn(category);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(new BigDecimal("500.25"), service.create(1L, input("500.25")).amount());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class); verify(repository).save(captor.capture());
        assertSame(user, captor.getValue().getUser()); assertSame(category, captor.getValue().getCategory());
    }
    @Test void updatePreservesOwnerAndChangesAmount() {
        Transaction transaction = new Transaction(); transaction.setUser(user);
        when(repository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(transaction));
        when(categories.require(1L, 2L, TransactionType.EXPENSE)).thenReturn(category); when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(new BigDecimal("700"), service.update(1L, 10L, input("700")).amount()); assertSame(user, transaction.getUser());
    }
    @Test void deletionLoadsByIdAndOwner() {
        Transaction transaction = new Transaction(); when(repository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(transaction));
        service.delete(1L, 10L); verify(repository).delete(transaction);
    }
    @Test void foreignOrMissingTransactionCannotBeUpdatedOrDeleted() {
        when(repository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(2L, 10L, input("700")));
        assertThrows(ResourceNotFoundException.class, () -> service.delete(2L, 10L)); verify(repository, never()).save(any()); verify(repository, never()).delete(any(Transaction.class));
    }
}
