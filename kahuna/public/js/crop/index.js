import angular from 'angular';

import './controller';
import '../components/gr-top-bar/gr-top-bar';

import cropTemplate from './view.html!text';


export var crop = angular.module('kahuna.crop', [
    'kahuna.crop.controller',
    'gr.topBar'
]);


crop.config(['$stateProvider',
             function($stateProvider) {

    $stateProvider.state('crop', {
        url: '/images/:imageId/crop',
        template: cropTemplate,
        controller: 'ImageCropCtrl',
        controllerAs: 'ctrl',
        resolve: {
            // TODO: abstract these resolvers out as we use them on the image
            // view too
            imageId: ['$stateParams', $stateParams => $stateParams.imageId],
            image: ['$state', '$q', 'mediaApi', 'mediaCropper', 'imageId',
                    ($state, $q, mediaApi, mediaCropper, imageId) => {

                return mediaApi.find(imageId).then(image => {
                    return mediaCropper.canBeCropped(image).then(croppable => {
                        if (croppable) {
                            return image;
                        } else {
                            $state.go('image-error', {message: 'Image cannot be cropped'});
                        }
                    });
                }).catch(error => {
                    if (error && error.status === 404) {
                        $state.go('image-error', {message: 'Image not found'});
                    } else {
                        return $q.reject(error);
                    }
                });
            }],
            optimisedImageUri: ['image', 'imgops', (image, imgops) => imgops.getUri(image)]
        }
    });

}]);
