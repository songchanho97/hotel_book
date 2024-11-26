import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GuestService guestService = new GuestService();
        AdminService adminService = new AdminService();

        System.out.println("로그인 유형을 선택하세요:");
        System.out.println("1. 게스트");
        System.out.println("2. 관리자");
        System.out.print("선택: ");
        int userType = scanner.nextInt();
        scanner.nextLine();

        if (userType == 2) {
            adminService.adminLogin(scanner);
        } else if(userType == 1) {

            System.out.println("게스트로 로그인합니다.");
            guestService.guestLogin(scanner);
        }
    }
}
