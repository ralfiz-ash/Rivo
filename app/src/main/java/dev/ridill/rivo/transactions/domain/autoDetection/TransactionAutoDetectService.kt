package dev.ridill.rivo.transactions.domain.autoDetection

import android.telephony.SmsMessage
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.di.ApplicationScope
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TransactionAutoDetectService(
    private val currencyPrefRepo: CurrencyRepository,
    private val extractor: TransactionDataExtractor,
    private val transactionRepo: TransactionRepository,
    private val notificationHelper: NotificationHelper<Transaction>,
    @ApplicationScope private val applicationScope: CoroutineScope
) {
    fun detectTransactionsFromMessages(messages: List<SmsMessage>) {
        val messagesFromOrg = messages
            .filter { extractor.isOriginValidOrg(it.displayOriginatingAddress.orEmpty()) }
        val dateTimeNow = DateUtil.now()
        applicationScope.launch {
            for (message in messagesFromOrg) {
                try {
//                    if (extractor.isSupportedLanguage(message.messageBody))
//                        throw UnsupportedLanguageThrowable(message.messageBody)

                    val data = extractor.extractData(message.messageBody)
                    if (data.paymentTimestamp.isAfter(dateTimeNow)) continue

                    val insertedTx = transactionRepo.saveTransaction(
                        amount = data.amount,
                        timestamp = data.paymentTimestamp,
                        note = data.note.orEmpty(),
                        type = data.transactionType
                    )
                    val parsedAmount = TextFormat.parseNumber(insertedTx.amount).orZero()

                    notificationHelper.postNotification(
                        id = insertedTx.id.hashCode(),
                        data = insertedTx.copy(
                            amount = TextFormat.currency(
                                parsedAmount,
                                currencyPrefRepo.getCurrencyPreferenceForMonth(dateTimeNow.toLocalDate())
                                    .first()
                            )
                        )
                    )
                } catch (t: TransactionDataExtractionFailedThrowable) {
                    Firebase.crashlytics.recordException(t)
                } catch (t: Throwable) {
                    Firebase.crashlytics.recordException(t)
                }
            }
        }
    }
}