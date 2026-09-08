public class TestDate {
    public static void main(String[] args) {
        java.time.LocalDateTime matchTime = java.time.LocalDateTime.now().minusDays(6);
        boolean isUnblurred = matchTime != null && matchTime.plusDays(5).compareTo(java.time.LocalDateTime.now()) <= 0;
        System.out.println("6 days ago: " + isUnblurred);

        matchTime = java.time.LocalDateTime.now().minusDays(4);
        isUnblurred = matchTime != null && matchTime.plusDays(5).compareTo(java.time.LocalDateTime.now()) <= 0;
        System.out.println("4 days ago: " + isUnblurred);
    }
}
