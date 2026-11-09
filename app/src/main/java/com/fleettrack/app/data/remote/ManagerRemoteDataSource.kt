package com.fleettrack.app.data.remote

import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SupabaseManagerDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("phone") val phone: String,
    @SerialName("hub_name") val hubName: String,
    @SerialName("pin_code") val pinCode: String = "1234",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("owner_id") val ownerId: String? = null
)

interface ManagerRemoteDataSource {
    suspend fun fetchManagers(): Result<List<SupabaseManagerDto>>
    suspend fun upsertManager(dto: SupabaseManagerDto): Result<Unit>
    suspend fun deleteManager(id: String): Result<Unit>
}

@Singleton
class ManagerRemoteDataSourceImpl @Inject constructor() : ManagerRemoteDataSource {
    override suspend fun fetchManagers(): Result<List<SupabaseManagerDto>> {
        return try {
            val list = SupabaseClientProvider.client.from("managers")
                .select()
                .decodeList<SupabaseManagerDto>()
            Result.success(list)
        } catch (e: Exception) {
            Timber.w(e, "Failed to fetch managers from Supabase, using offline cache")
            Result.failure(e)
        }
    }

    override suspend fun upsertManager(dto: SupabaseManagerDto): Result<Unit> {
        return try {
            SupabaseClientProvider.client.from("managers").upsert(dto)
            Timber.d("Upserted manager to Supabase: ${dto.name} (${dto.hubName})")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.w(e, "Manager upsert offline fallback")
            Result.failure(e)
        }
    }

    override suspend fun deleteManager(id: String): Result<Unit> {
        return try {
            SupabaseClientProvider.client.from("managers").delete {
                filter { eq("id", id) }
            }
            Timber.d("Deleted manager from Supabase: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.w(e, "Manager delete offline fallback")
            Result.failure(e)
        }
    }
}

