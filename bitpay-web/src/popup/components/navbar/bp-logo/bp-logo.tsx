import React from 'react';
import './bp-logo.scss';
import { motion } from 'framer-motion';
import { FrameDimensions } from '../../../../services/frame';

const animateLogo = {
  base: {
    opacity: 1,
    x: 0,
    y: 0,
    scale: 1,
    fill: '#1a3b8b',
    transition: {
      type: 'spring',
      damping: 30,
      stiffness: 100,
      mass: 0.75
    }
  },
  solo: {
    opacity: 1,
    x: FrameDimensions.width / 2 - 7,
    y: 3,
    scale: 1.4,
    fill: '#1a3b8b',
    transition: {
      type: 'spring',
      damping: 30,
      stiffness: 100,
      mass: 0.75
    }
  },
  payMode: {
    opacity: 1,
    x: 52,
    y: 0,
    scale: 1,
    fill: '#ffffff',
    transition: {
      type: 'spring',
      damping: 30,
      stiffness: 100,
      mass: 0.75
    }
  },
  payModeHelper: {
    opacity: 1,
    x: 8,
    y: -0.5,
    transition: {
      type: 'spring',
      damping: 30,
      stiffness: 100,
      mass: 0.75,
      delay: 0.3
    }
  },
  hidden: (i: number): Record<string, unknown> => ({
    opacity: 0,
    transition: {
      type: 'spring',
      damping: 20,
      stiffness: 200,
      mass: 0.25,
      delay: i * 0.025
    }
  }),
  show: (i: number): Record<string, unknown> => ({
    opacity: 1,
    transition: {
      type: 'spring',
      damping: 30,
      stiffness: 50,
      mass: 1,
      delay: i * 0.025 + 0.05
    }
  })
};

const BitpayLogo: React.FC<{ solo?: boolean; payMode?: boolean }> = ({ solo, payMode = false }) => (
  <>
    {payMode && (
      <motion.div
        className="bp-logo__helper"
        initial="hidden"
        animate="payModeHelper"
        variants={animateLogo}
        custom={0}
      >
        Pay with
      </motion.div>
    )}
    <motion.svg
      className="bp-logo"
      initial="base"
      // eslint-disable-next-line no-nested-ternary
      animate={payMode ? 'payMode' : solo ? 'solo' : 'base'}
      variants={animateLogo}
      fill="#1a3b8b"
      opacity="1"
      height="18"
      viewBox="0 0 79.667 28"
    >
      <path
        id="1_B"
        data-name="1_B"
        d="M83.3,79.748a5.283,5.283,0,0,1,2.314.484,4.621,4.621,0,0,1,1.62,1.283,5.717,5.717,0,0,1,.947,1.893,8.247,8.247,0,0,1,.316,2.335,9.281,9.281,0,0,1-.715,3.639,9.622,9.622,0,0,1-1.935,2.987A9.034,9.034,0,0,1,79.322,95.1c-.168,0-.463,0-.884-.021a9.2,9.2,0,0,1-1.431-.126c-.547-.084-1.115-.189-1.7-.337a8.591,8.591,0,0,1-1.7-.61l4.817-20.237L82.73,73.1l-1.7,7.174a5.63,5.63,0,0,1,1.094-.379A4.2,4.2,0,0,1,83.3,79.748ZM79.68,91.654a3.508,3.508,0,0,0,1.83-.484A5.032,5.032,0,0,0,83,89.908a6.179,6.179,0,0,0,.989-1.788,6.373,6.373,0,0,0,.358-2.083,4.142,4.142,0,0,0-.442-2.083,1.838,1.838,0,0,0-1.7-.736,3.648,3.648,0,0,0-.947.126,2.394,2.394,0,0,0-1.115.589L78.333,91.57A9.3,9.3,0,0,0,79.68,91.654Z"
        transform="translate(-73.6 -73.1)"
      />
      <motion.g
        initial="show"
        animate={!payMode && solo ? 'hidden' : 'show'}
        variants={animateLogo}
        custom={solo ? 4 : 0}
        opacity="1"
        id="2_I"
        data-name="2_I"
        transform="translate(15.21 2.188)"
      >
        <path id="i_DOT" d="M169.444,86.172l.631-2.672h-4.144l-.631,2.672Z" transform="translate(-161.219 -83.5)" />
        <path id="i_BODY" d="M149.56,104.6l-3.66,15.357h4.144l3.639-15.357Z" transform="translate(-145.9 -100.161)" />
      </motion.g>
      <motion.path
        initial="show"
        animate={!payMode && solo ? 'hidden' : 'show'}
        variants={animateLogo}
        custom={solo ? 3 : 1}
        opacity="1"
        id="3_T"
        data-name="3_T"
        d="M189.842,99.761a3,3,0,0,1-1.262-.231,1.312,1.312,0,0,1-.652-.652,2.09,2.09,0,0,1-.147-.989,6.753,6.753,0,0,1,.21-1.22l1.262-5.259h4.712l.863-3.492h-4.733l1.094-4.418-4.418.694-2.8,11.823a14.974,14.974,0,0,0-.4,2.777,5.2,5.2,0,0,0,.4,2.335,3.413,3.413,0,0,0,1.536,1.6,6.385,6.385,0,0,0,3.05.589,11.233,11.233,0,0,0,2.377-.231c.063-.021.168-.042.231-.063l.863-3.6a4.673,4.673,0,0,1-.863.21A6.745,6.745,0,0,1,189.842,99.761Z"
        transform="translate(-160.433 -81.312)"
      />
      <motion.path
        initial="show"
        animate={!payMode && solo ? 'hidden' : 'show'}
        variants={animateLogo}
        custom={2}
        opacity="1"
        id="4_P"
        data-name="4_P"
        d="M241.746,110.759a10.222,10.222,0,0,1-.673,3.745,8.612,8.612,0,0,1-1.872,2.924,8.444,8.444,0,0,1-2.861,1.914,9.319,9.319,0,0,1-3.618.694,11.976,11.976,0,0,1-1.914-.168l-1.262,5.07H225.4l4.817-20.237h5.533a6.846,6.846,0,0,1,2.693.484,4.97,4.97,0,0,1,2.945,3.24A6.97,6.97,0,0,1,241.746,110.759ZM231.585,116.5a5.646,5.646,0,0,0,3.177-.295,4.568,4.568,0,0,0,1.536-1.136,5.639,5.639,0,0,0,.989-1.746,6.92,6.92,0,0,0,.358-2.251,3.683,3.683,0,0,0-.526-2.041,2.013,2.013,0,0,0-1.851-.862h-1.641Z"
        transform="translate(-193.466 -98.052)"
      />
      <motion.path
        initial="show"
        animate={!payMode && solo ? 'hidden' : 'show'}
        variants={animateLogo}
        custom={solo ? 1 : 3}
        opacity="1"
        id="5_A"
        data-name="5_A"
        d="M321.4,113.167a9.547,9.547,0,0,0-.231,3.177,12.655,12.655,0,0,0,.926,3.513h-4a5.053,5.053,0,0,1-.568-1.473,7.573,7.573,0,0,1-1.725,1.052,5.245,5.245,0,0,1-2.125.421,5.441,5.441,0,0,1-2.377-.484,4.491,4.491,0,0,1-1.62-1.3,5.2,5.2,0,0,1-.9-1.914,9.4,9.4,0,0,1-.273-2.356,9.873,9.873,0,0,1,.673-3.618,9.416,9.416,0,0,1,1.893-2.966,8.107,8.107,0,0,1,5.953-2.714h6.416Zm-2.924-5.154c-1.935,0-2.293,0-3.092.4a4.492,4.492,0,0,0-1.43,1.241,5.962,5.962,0,0,0-.968,1.767,6.373,6.373,0,0,0-.358,2.083,4.372,4.372,0,0,0,.442,2.125,1.707,1.707,0,0,0,1.641.778,2.6,2.6,0,0,0,1.157-.252,4.018,4.018,0,0,0,1.094-.841q.063-.694.189-1.452c.084-.5.189-.989.274-1.409l1.052-4.439"
        transform="translate(-259.084 -97.894)"
      />
      <motion.path
        initial="show"
        animate={!payMode && solo ? 'hidden' : 'show'}
        variants={animateLogo}
        custom={solo ? 0 : 4}
        opacity="1"
        id="6_Y"
        data-name="6_Y"
        d="M395.183,104.442h-4.144l-2.4,10.119-.337,1.388c-.231.063-.463.126-.694.168a6.747,6.747,0,0,1-1.325.126,3,3,0,0,1-1.262-.231,1.312,1.312,0,0,1-.652-.652,2.09,2.09,0,0,1-.147-.989,6.743,6.743,0,0,1,.21-1.22l1.262-5.259.82-3.492h-4.228l-1.914,8.078a14.977,14.977,0,0,0-.4,2.777,5.2,5.2,0,0,0,.4,2.335,3.412,3.412,0,0,0,1.536,1.6,6.384,6.384,0,0,0,3.05.589,11.233,11.233,0,0,0,2.377-.231c.021,0,.063-.021.084-.021a3.736,3.736,0,0,1-1.157,2.041,3.973,3.973,0,0,1-2.672.8,9.47,9.47,0,0,1-1.388-.084l-.82,3.429a15.922,15.922,0,0,0,1.83.105,11.888,11.888,0,0,0,3.3-.421,6.806,6.806,0,0,0,2.44-1.3,7.734,7.734,0,0,0,1.7-2.188,12.694,12.694,0,0,0,1.094-3.134l2.861-12.033Z"
        transform="translate(-315.517 -97.815)"
      />
    </motion.svg>
  </>
);

export default BitpayLogo;
