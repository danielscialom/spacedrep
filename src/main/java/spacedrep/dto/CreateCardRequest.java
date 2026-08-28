package spacedrep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCardRequest {

    @NotBlank(message = "Front cannot be blank")
    @Size(max = 500, message = "Front must be up to 500 characters")
    private String front;

    @NotBlank(message = "Back cannot be blank")
    @Size(max = 500, message = "Back must be up to 500 characters")
    private String back;

    // Default constructor (required for Spring's JSON conversion mechanism)
    public CreateCardRequest() {
    }

    public CreateCardRequest(String front, String back) {
        this.front = front;
        this.back = back;
        System.out.println("tamir");
    }

    public String getFront() {
        return front;
    }

    public void setFront(String front) {
        this.front = front;
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }
}
