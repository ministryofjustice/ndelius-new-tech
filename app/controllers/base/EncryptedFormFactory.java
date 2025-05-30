package controllers.base;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.function.Function;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.typesafe.config.Config;
import play.data.FormFactory;
import play.data.format.Formatters;
import play.i18n.MessagesApi;

@Singleton
public class EncryptedFormFactory extends FormFactory {

    private final MessagesApi messagesApi;
    private final Formatters formatters;
    private final ValidatorFactory validatorFactory;
	private final Config config;

	@Inject
    public EncryptedFormFactory(MessagesApi messagesApi, Formatters formatters, ValidatorFactory validatorFactory, Config config) {

        super(messagesApi, formatters, validatorFactory, config);

        this.messagesApi = messagesApi;
        this.formatters = formatters;
        this.validatorFactory = validatorFactory;
		this.config = config;
	}

    public <T> EncryptedForm<T> form(Class<T> clazz, Function<Map<String, String>, Map<String, String>> decrypter) {

        return new EncryptedForm<>(clazz, decrypter, messagesApi, formatters, validatorFactory, config);
    }
}
