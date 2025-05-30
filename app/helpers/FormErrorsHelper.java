package helpers;

import play.data.Form;
import play.data.validation.ValidationError;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormErrorsHelper {
	public static List<ValidationError> allErrors(Form<?> form) {
		return Stream.concat(form.errors().stream(), form.globalErrors().stream()).collect(Collectors.toList());
	}
}