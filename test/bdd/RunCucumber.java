package bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features={"features/bdd"}, plugin = {"pretty", "rerun:target/cucumber-rerun.txt"})
public class RunCucumber {
}