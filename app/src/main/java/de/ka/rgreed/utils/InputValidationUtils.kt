package de.ka.rgreed.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import de.ka.rgreed.R

/**
 * Lists different validation rules.
 */
enum class ValidationRules(
    val variable: Int? = null, val predicate: String.() -> Boolean, @StringRes val errorTextResId: Int
) {
    NOT_EMPTY(predicate = { isBlank() }, errorTextResId = R.string.error_input_empty),
    MIN_4(variable = 4, predicate = { length < 4 }, errorTextResId = R.string.error_input_too_small),
    MIN_8(variable = 8, predicate = { length < 8 }, errorTextResId = R.string.error_input_too_small),
    MAX_8(variable = 8, predicate = { length > 8 }, errorTextResId = R.string.error_input_too_long),
}

/**
 * A input class for validating with a error output. Should at least have one [rules] to have any effect.
 */
data class ValidatorInput(val input: String, val errorOutPut: MutableLiveData<String>, val rules: List<ValidationRules>)

/**
 * A simple input validator, offering a [validateAll] method for quick error setting and retrieving the validation state
 * of inputs.
 */
class InputValidator(private val validationPairs: List<ValidatorInput>) {

    /**
     * Checks whether all validations are true or at least one is failing.
     *
     * @param context the context to show errors on
     * @return true if all fields defined in the [validationPairs] are valid, false if at least one is failing
     */
    fun validateAll(context: Context): Boolean {

        var isValid = true

        validationPairs.forEach pairs@{ pair ->
            pair.rules.forEach { rule ->
                if (rule.predicate(pair.input)) {
                    isValid = false
                    if (rule.variable != null) {
                        pair.errorOutPut.postValue(String.format(context.getString(rule.errorTextResId), rule.variable))
                    } else {
                        pair.errorOutPut.postValue(context.getString(rule.errorTextResId))
                    }
                    return@pairs
                }
            }
        }
        return isValid
    }

}