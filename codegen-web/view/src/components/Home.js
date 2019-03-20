import React, {Component} from 'react';
import {withStyles} from "@material-ui/core";

const styles = theme => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    pre: {
        'white-space': 'pre-wrap',
    },
});

class Home extends Component {
    render() {
        const { classes } = this.props;
        return (
            <div>
                <pre className={classes.pre}>
                    Light-codegen can be used to generate a single project or multiple projects in a folder. Please select from the generator menu.
                </pre>
            </div>
        )
    }
}

export default withStyles(styles)(Home);