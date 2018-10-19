from django.urls import path
from . import views

urlpatterns = [
    path(
        'create_session',
        views.CreateSession.as_view(),
        name='create_session'),
    path(
        'subscribe_session/<str:session_id>',
        views.SubscribeToSession.as_view(),
        name='subscribe_session'),
    path(
        'videos/<str:archive_id>',
        views.GetVideos.as_view(),
        name='get_videos')
]
