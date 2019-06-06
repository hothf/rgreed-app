package de.ka.rgreed

import de.ka.rgreed.repo.api.models.ConsensusResponse
import de.ka.rgreed.utils.addAllUniqueIds
import org.junit.Test

import org.junit.Assert.*
import kotlin.system.measureTimeMillis

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UnitTests {

    @Test
    fun addingConsensusesPerformance() {

        val sourceList = mutableListOf<ConsensusResponse>()

        val take = 20_000
        var hasSeedExemption = false

        sourceList.addAll(generateSequence(
            seed = 1,
            nextFunction = { seed ->
                if (seed % 10 == 0 && !hasSeedExemption) {
                    hasSeedExemption = true
                    seed
                } else {
                    hasSeedExemption = false
                    seed + 1
                }
            })
            .take(take)
            .map { sequence ->
                ConsensusResponse(
                    id = sequence,
                    title = sequence.toString(),
                    admin = false,
                    suggestionsCount = 0,
                    public = true,
                    creator = "creator",
                    creationDate = 0,
                    votingStartDate = 0,
                    description = null,
                    endDate = 0,
                    voters = listOf(),
                    finished = false,
                    following = false,
                    hasAccess = true
                )
            })
        val newList = mutableListOf<ConsensusResponse>()

        val measuredTimeMillis1 = measureTimeMillis {
            newList.addAllUniqueIds(sourceList)
        }

        println("Measured time (first - duplicates) 1: $measuredTimeMillis1 ms")

        newList.clear()
        val measuredTimeMillis2 = measureTimeMillis {
            newList.addAllUniqueIds(sourceList)
        }

        println("Measured time (again - no duplicates) 2: $measuredTimeMillis2 ms")

        //        pretty printing
        //        println("Source: " + sourceList.joinToString { it.title })
        //        println(" for: " + newList.joinToString { it.title })

        assertTrue(sourceList.size > newList.size)
    }
}
