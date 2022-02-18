import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import space.kscience.kmath.distributions.*
import space.kscience.kmath.stat.RandomGenerator
import space.kscience.kmath.stat.UniformDistribution
import space.kscience.kmath.stat.next
import kotlin.math.PI
import kotlin.math.exp

object MonteCarlo {


    @JvmStatic
    fun main(args: Array<String>) = runBlocking {

        val n = 100000

        // When using normal Monte-Carlo (on uniform distribution) algorithm is very unstable
        // If `s` is too low (0-2), results is incorrect because we're not integrating the whole space
        // If `s` is too high (about 5-10), numerical precision isn't enough, and we get 0 as a result

        val s = 3.0
        val uniform2d = FactorizedDistribution(
            listOf(
                NamedDistributionWrapper("x", UniformDistribution(-s..s)),
                NamedDistributionWrapper("y", UniformDistribution(-s..s))
            )
        )

        val estimateUniform = monteCarlo(n, uniform2d) {
            val x = it["x"]!!
            val y = it["y"]!!
            if (x <= 0 || y <= 0) {
                0.0
            } else {
                exp(-((x * x) + (y * y)))
            }
        }

        // Using importance-sampling and matching distribution (i. e Normal for this case) it's possibly to lessen hyperparamter sensitivity
        val normal2d = FactorizedDistribution(
            listOf(
                NamedDistributionWrapper("x", NormalDistribution(0.0, 1.0)),
                NamedDistributionWrapper("y", NormalDistribution(0.0, 1.0))
            )
        )

        val estimate1 = monteCarlo(n, normal2d) {
            val x = it["x"]!!
            val y = it["y"]!!
            if (x <= 0 || y <= 0) {
                0.0
            } else {
                // computing the relevant function directly
                exp(-((x * x) + (y * y)))
            }
        }

        val estimate2 = monteCarloDirect(n, normal2d) {
            val x = it["x"]!!
            val y = it["y"]!!
            if (x <= 0 || y <= 0) {
                0.0
            } else {
                //inlined importance sampling
                2 * exp(-((x * x) + (y * y)) / 2) * PI
            }
        }

        val actual = PI / 4
        println("Uniform estimate: ${estimateUniform - actual}")
        println("Importance-sampling: ${estimate1 - actual}")
        println("Inlined importance-sampling: ${estimate2 - actual}")

    }

    /**
     * Importance-sampling as part of ab algorithm
     */
    private suspend fun <T : Any> monteCarlo(n: Int, sampler: Distribution<T>, function: (T) -> Double): Double {
        return sampler.sample(RandomGenerator.default)
            .take(n)
            .map { x -> function(x) / (n * sampler.probability(x)) }
            .fold(0.0) { x, y -> x + y }
    }

    /**
     * No importance-sampling in algorithm, allows to inline it into `function`
     */
    private suspend fun <T : Any> monteCarloDirect(n: Int, sampler: Distribution<T>, function: (T) -> Double): Double {
        return sampler.sample(RandomGenerator.default)
            .take(n)
            .map { x -> function(x) / n }
            .fold(0.0) { x, y -> x + y }
    }

}