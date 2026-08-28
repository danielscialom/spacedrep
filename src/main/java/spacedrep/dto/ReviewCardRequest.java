package spacedrep.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReviewCardRequest {

    @NotNull(message = "Grade cannot be null")
    @Min(value = 0, message = "Grade must be between 0 and 5")
    @Max(value = 5, message = "Grade must be between 0 and 5")
    private Integer grade;

    // Default constructor (required for Spring's JSON conversion mechanism)
    public ReviewCardRequest() {
    }

    public ReviewCardRequest(Integer grade) {
        this.grade = grade;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }
}
