from django.views.generic import View
from django.http import JsonResponse
from opentok import OpenTok
from opentok import MediaModes
from opentok import ArchiveModes
from django.conf import settings
import datetime


# Create your views here.


class CreateSession(View):

    def get(self, request, *args, **kwargs):
        opentok = OpenTok(
            settings.API_KEY,
            settings.API_SECRET)
        session = opentok.create_session(
            media_mode=MediaModes.routed,
            archive_mode=ArchiveModes.always)
        response = {
            'session_id': session.session_id,
            'api_key': settings.API_KEY,
            'token': opentok.generate_token(session.session_id)
        }
        return JsonResponse(
            response, content_type="application/json", safe=False)


class SubscribeToSession(View):

    def get(self, request, *args, **kwargs):
        opentok = OpenTok(
            settings.API_KEY,
            settings.API_SECRET)
        session_id = kwargs.get('session_id')
        response = {
            'api_key': settings.API_KEY,
            'token': opentok.generate_token(session_id)
        }
        return JsonResponse(
            response, content_type="application/json", safe=False)


class GetVideos(View):

    def get(self, request, *args, **kwargs):
        opentok = OpenTok(
            settings.API_KEY,
            settings.API_SECRET)
        response = opentok.get_archive(kwargs.get('archive_id'))
        millis = int(response.duration)
        seconds = (millis / 1000) % 60
        seconds = int(seconds)
        minutes = (millis / (1000 * 60)) % 60
        minutes = int(minutes)
        hours = (millis / (1000 * 60 * 60)) % 24
        response = {
            "url": response.url,
            "status": response.status,
            "api_key": settings.API_KEY,
            "length": str(hours) + " : " + str(minutes) + " : " + str(seconds)}
        return JsonResponse(
            response, content_type="application/json", safe=False)
