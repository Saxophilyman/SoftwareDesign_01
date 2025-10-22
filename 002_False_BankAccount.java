public class BankAccount {
    private double balance;

    public BankAccount(double initialBalance) {
        this.balance = initialBalance; 
    }

    // нет проверки на отрицательную сумму
    public void deposit(double amount) {
        balance += amount;
    }

    // нет проверки на отрицательную сумму
    // баланс может стать отрицательным
    public void withdraw(double amount) {
        balance -= amount;
    }

    public double getBalance() {
        return balance;
    }
}



import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BankAccountTest {
    @Test
    void goodTest() {
        BankAccount acc = new BankAccount(100.0);

        assertDoesNotThrow(() -> acc.deposit(50.0));
        assertDoesNotThrow(() -> acc.withdraw(70.0));

        assertEquals(80.0, acc.getBalance(), 1e-9); // выглядит как всё хорошо
    }

    @Test
    void badTestDeposit() {
        BankAccount acc = new BankAccount(100.0);
        acc.deposit(-200.0); // логическая ошибка: депозит уменьшает баланс
        assertEquals(-100.0, acc.getBalance(), 1e-9); // тест ПРОХОДИТ
    }

    @Test
    void badTestWithdraw() {
        BankAccount acc = new BankAccount(50.0);
        acc.withdraw(10_000.0); // логическая ошибка: баланс уходит в минус
        assertTrue(acc.getBalance() < 0.0); // тест ПРОХОДИТ
    }
}
