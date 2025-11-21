/*В целом предыдущее решение было правильным, кроме функции, где массив проверяется что он не пустой
quickSort()

Пока возьму за основу

Предусловие P: arr! = null. Массив не равен null
Постусловие Q: result = sorted(arr, 0, arr.length-1). Результатом должен быть отсортированный массив

Инвариант цикла -- это логическое утверждение, которое остается истинным до и после каждой итерации цикла. 

Пожалуй мне придётся немного упростить себе задачу, взяв наоборот относительно простой вариант быстрой сортировки, чтобы не запутаться
Общий принцип быстрой сортировки деление отрезка на части(условно половину), в котором левые элементы будут меньше опорного (pivot),
а правые больше. В основном применяется рекурсия, таким образом, что массив любой длинны делится на всё более мелкие части, уже отсортированные в определённом порядке.
Исходный массив меняется "на месте". Есть разные реализации, и более привычный length/2, но в данном случае я попытаюсь рассмотреть вариант с разбиением Ломуто

public class QuickSort {

    public static void quickSort(int[] arr) {
        if (arr == null || arr.length <= 1) return;
        quickSort(arr, 0, arr.length - 1);
    }

    private static void quickSort(int[] arr, int left, int right) {
        if (left < right) {
            int p = partition(arr, left, right);
            quickSort(arr, left, p - 1);
            quickSort(arr, p + 1, right);
        }
    }

    // Разбиение Ломуто
    private static int partition(int[] arr, int left, int right) {
        int pivot = arr[right];       
        int i = left - 1;            

        for (int j = left; j < right; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }

        swap(arr, i + 1, right);
        return i + 1;
    }

    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}

Началом будет общее предусловие, что массив не равен нулю. 
Стоит ли здесь рассматривать quickSort как состоящую из 3х частей? quickSort, partition, swap?
Рассмотрим quickSort. 
Пред-условие
Каждый вызов должен удовлетворять условию arr != null и 0 <= left < right < arr.length (условно массив на отрезке)

*/
