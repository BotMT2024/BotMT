import java.sql.SQLOutput;

public class Main {
    public static void main(String[] args) {
        ListNode l13 = new ListNode(3);
        ListNode l12 = new ListNode(4, l13);
        ListNode l11 = new ListNode(2, l12);

        ListNode l23 = new ListNode(4);
        ListNode l22 = new ListNode(6, l23);
        ListNode l21 = new ListNode(5, l22);

        ListNode out = addTwoNumbers(l11, l21);

        System.out.println(out.val);
        System.out.println();
    }

    public static ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        sb1.append(l1.val);

        while (l1.next != null) {
            sb1.append(l1.next.val);
            l1 = l1.next;
        }

        sb2.append(l2.val);

        while (l2.next != null) {
            sb2.append(l2.next.val);
            l2 = l2.next;
        }

        sb1.reverse();
        sb2.reverse();

        String answer = Long.parseLong(sb1.toString()) + Long.parseLong(sb2.toString()) + "";

        String[] out = answer.split("");

        ListNode[] ls = new ListNode[out.length];
        ls[0] = new ListNode(Integer.parseInt(out[0]));

        for (int i = 1; i < out.length; i++) {
            ls[i] = new ListNode(Integer.parseInt(out[i]), ls[i - 1]);
        }

        return ls[out.length - 1];
    }

}


class ListNode {
    int val;
    ListNode next;

    ListNode() {
    }

    ListNode(int val) {
        this.val = val;
    }

    ListNode(int val, ListNode next) {
        this.val = val;
        this.next = next;
    }
}