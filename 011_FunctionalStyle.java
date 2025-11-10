//Бронирование места в зале
//ООП
class SeatMapOop {
    private final boolean[] taken;
    public SeatMapOop(int seats) { this.taken = new boolean[seats]; }

    public synchronized boolean reserve(int idx) {
        check(idx);
        if (taken[idx]) return false;      // занято
        taken[idx] = true;                  
        return true;
    }
    public synchronized void cancel(int idx) {
        check(idx);
        if (!taken[idx]) throw new IllegalStateException("not reserved");
        taken[idx] = false;                  
    }
    private void check(int i){ if (i<0 || i>=taken.length) throw new IndexOutOfBoundsException(); }
}

//ФП
//типа возвращает новый HallFp.
import java.util.*;

record HallFp(int total, Set<Integer> booked) {
    static HallFp of(int total) { return new HallFp(total, Set.of()); }
}
sealed interface ResHall { record Ok(HallFp hall) implements ResHall {} record Err(String msg) implements ResHall {} }

final class HallOps {
    static ResHall reserve(HallFp h, int idx) {
        if (idx<0 || idx>=h.total()) return new ResHall.Err("bad index");
        if (h.booked().contains(idx))    return new ResHall.Err("already booked");
        var nb = new HashSet<>(h.booked()); nb.add(idx);
        return new ResHall.Ok(new HallFp(h.total(), Set.copyOf(nb))); // НОВОЕ значение
    }
    static ResHall cancel(HallFp h, int idx) {
        if (!h.booked().contains(idx)) return new ResHall.Err("not booked");
        var nb = new HashSet<>(h.booked()); nb.remove(idx);
        return new ResHall.Ok(new HallFp(h.total(), Set.copyOf(nb)));
    }
}
