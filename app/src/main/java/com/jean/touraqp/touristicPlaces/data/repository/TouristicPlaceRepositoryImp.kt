package com.jean.touraqp.touristicPlaces.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.jean.touraqp.core.TourAqpDB
import com.jean.touraqp.core.constants.DBCollection
import com.jean.touraqp.core.seed.TouristicPlacesSeed
import com.jean.touraqp.core.utils.ResourceResult
import com.jean.touraqp.touristicPlaces.data.local.entities.TouristicPlaceEntity
import com.jean.touraqp.touristicPlaces.data.mapper.toTouristicPlace
import com.jean.touraqp.touristicPlaces.data.mapper.toTouristicPlaceEntity
import com.jean.touraqp.touristicPlaces.data.remote.dto.TouristicPlaceDto
import com.jean.touraqp.touristicPlaces.domain.TouristicPlaceRepository
import com.jean.touraqp.touristicPlaces.domain.model.TouristicPlace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TouristicPlaceRepositoryImp @Inject constructor(
    private val remoteDB: FirebaseFirestore,
    private val localDB: TourAqpDB

) : TouristicPlaceRepository {

    companion object {
        const val TAG = "touristic_place_repository"
    }

    private val touristicPlacesCollection = remoteDB.collection(DBCollection.TOURISTIC_PLACE)
    private val touristicPlaceDao = localDB.getTouristicPlaceDao()

    override suspend fun getAllTouristicPlaces(fetchFromNetwork: Boolean): Flow<ResourceResult<List<TouristicPlace>>> =
        flow {
            try {
                Log.d(TAG, "CALLING ")
                emit(ResourceResult.Loading())

                val touristicPlaces = if (fetchFromNetwork) {
                    //Get from network
                    val result = touristicPlacesCollection.limit(3).get().await()
                    val places = result.documents

                    val touristicPlaces = places.map() { touristicPlace ->
                        //Convert to DTO
                        val touristicPlaceDto = touristicPlace.toObject<TouristicPlaceDto>()
                            ?: throw Exception("Data mismatch")
                        //Store in Room
                        val touristicPlaceEntity =
                            touristicPlaceDto.toTouristicPlaceEntity(id = touristicPlace.id)
                        touristicPlaceDao.insert(touristicPlaceEntity)


                        return@map touristicPlaceDto.toTouristicPlace(touristicPlace.id)
                    }

                    touristicPlaces
                } else {
                    // Get from ROOM
                    val touristicPlacesEntities = touristicPlaceDao.getAllTouristicPlaces()
                    val touristicPlaces = touristicPlacesEntities.map { touristicPlaceEntity ->
                        touristicPlaceEntity.toTouristicPlace()
                    }
                    touristicPlaces
                }

                Log.d(TAG, "$touristicPlaces")
                //Send results
                emit(
                    ResourceResult.Success(
                        data = touristicPlaces,
                        message = "Successful Query"
                    )
                )
            } catch (e: Exception) {
                Log.d(TAG, "${e.message}")
                emit(
                    ResourceResult.Success(
                        message = "Something went wrong"
                    )
                )
            }
        }

    override suspend fun getTouristicPlaceDetail(id: String): Flow<ResourceResult<TouristicPlace>> =
        flow {
            try {
                emit(ResourceResult.Loading(message = "Loading..."))
                val docRef = touristicPlacesCollection.document(id)
                val result = docRef.get().await()
                val placeDto =
                    result.toObject<TouristicPlaceDto>() ?: throw Exception("Data Mismatch")
                val place = placeDto.toTouristicPlace(id)

                emit(
                    ResourceResult.Success(
                        data = place,
                        message = "Successful Query"
                    )
                )
            } catch (e: Exception) {
                Log.d(TAG, "${e.message}")
                emit(
                    ResourceResult.Success(
                        message = "Something went wrong"
                    )
                )
            }
        }
}