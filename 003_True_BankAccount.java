
public class BankAccount {
    private double balance;

    public BankAccount(double initialBalance) {
        if (initialBalance < 0.0) {
            throw new IllegalArgumentException("Начальный баланс не может быть отрицательным");
        }
        this.balance = initialBalance;
    }

    public void deposit(double amount) {
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Сумма депозита должна быть > 0");
        }
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Сумма снятия должна быть > 0");
        }
        if (amount > balance) {
            throw new IllegalStateException("Недостаточно средств на счёте");
        }
        balance -= amount;
    }

    public double getBalance() {
        return balance;
    }
}

public class Main {
    public static void main(String[] args) {
        BankAccount account = new BankAccount(1000);
        System.out.println("Начальный баланс: " + account.getBalance());

        account.deposit(500);
        System.out.println("Баланс после депозита 500: " + account.getBalance());

        account.withdraw(200);
        System.out.println("Баланс после снятия 200: " + account.getBalance());

        // Операции, которые раньше были неправильными
        try {
            account.withdraw(2000);
        } catch (RuntimeException e) {
            System.out.println("withdraw(2000) ошибка: " + e.getMessage());
        }

        try {
            account.deposit(-100);
        } catch (RuntimeException e) {
            System.out.println("deposit(-100) ошибка: " + e.getMessage());
        }

        try {
            account.withdraw(-50);
        } catch (RuntimeException e) {
            System.out.println("withdraw(-50) ошибка: " + e.getMessage());
        }

        System.out.println("Итоговый баланс: " + account.getBalance());
    }
}
