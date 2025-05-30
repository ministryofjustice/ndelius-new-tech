package controllers.base;

import com.typesafe.config.Config;
import play.data.Form;
import play.data.format.Formatters;
import play.i18n.MessagesApi;
import play.mvc.Http;

import javax.validation.ValidatorFactory;
import java.util.Map;
import java.util.function.Function;

public class EncryptedForm<T> extends Form<T> {

    private final Function<Map<String, String>, Map<String, String>> decrypter;

    public EncryptedForm(Class<T> clazz, Function<Map<String, String>, Map<String, String>> decrypter, MessagesApi messagesApi, Formatters formatters, ValidatorFactory validatorFactory, Config config) {

        super(clazz, messagesApi, formatters, validatorFactory, config);

        this.decrypter = decrypter;
    }

    @Override
    protected Map<String, String> requestData(Http.Request request) {

        return decrypter.apply(super.requestData(request));
    }
}
